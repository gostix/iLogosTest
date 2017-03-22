package com.testing.services;

import com.testing.listener.OnFileChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * A simple class which can monitor files and notify interested parties
 * listeners of file changes.
 */
public class SimpleDirectoryWatchService implements DirectoryWatchService
{

    private static final Logger LOGGER = LogManager.getLogger(SimpleDirectoryWatchService.class);
    private final WatchService watchService;
    private final ConcurrentMap<WatchKey, Path> watchKeyPathConcurrentMap;
    private final ConcurrentMap<Path, Set<OnFileChangeListener>> pathSetConcurrentMap;
    private final ConcurrentMap<OnFileChangeListener, Set<PathMatcher>> onFileChangeListenerSetConcurrentMap;

    /**
     * Notifies the implementation of <em>OnFileChangeListener</em> interface that
     * should be monitored for file system events. If the changed file matches any
     * of the <code>globPatterns</code>, <code>listener</code> should be notified.
     *
     * @param listener     the on file change listener
     * @param dirPath      the monitoring dir path
     * @param globPatterns the glob patterns
     * @throws IOException If an I/O error occurs.
     */
    public SimpleDirectoryWatchService(OnFileChangeListener listener, String dirPath, String... globPatterns) throws IOException
    {
        watchService = FileSystems.getDefault().newWatchService();
        watchKeyPathConcurrentMap = new ConcurrentHashMap<>();
        pathSetConcurrentMap = new ConcurrentHashMap<>();
        onFileChangeListenerSetConcurrentMap = new ConcurrentHashMap<>();

        Path dir = Paths.get(dirPath);

        if (!Files.isDirectory(dir))
        {
            throw new IllegalArgumentException(dirPath + " is not a directory.");
        }

        if(Files.isSymbolicLink(dir) ){
            throw new IllegalArgumentException(dirPath + " symbolic links is not supported.");
        }

        if (!pathSetConcurrentMap.containsKey(dir))
        {
            WatchKey key = dir.register(watchService, ENTRY_CREATE);
            watchKeyPathConcurrentMap.put(key, dir);
            pathSetConcurrentMap.put(dir, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        }

        pathSetConcurrentMap.get(dir).add(listener);

        Set<PathMatcher> patterns = Collections.newSetFromMap(new ConcurrentHashMap<>());

        for (String globPattern : globPatterns)
        {
            patterns.add(matcherForGlobExpression(globPattern));
        }

        if (patterns.isEmpty())
        {
            patterns.add(matcherForGlobExpression("*")); // Match everything if no filter is found
        }

        onFileChangeListenerSetConcurrentMap.put(listener, patterns);

        LOGGER.info("Watching files matching " + Arrays.toString(globPatterns) + " under " + dirPath + " for changes.");
    }

    /**
     * Matcher for glob expression path matcher.
     *
     * @param globPattern the glob pattern
     * @return the path matcher
     */
    private static PathMatcher matcherForGlobExpression(String globPattern)
    {
        return FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
    }

    /**
     * Tells if given path matches this matcher's patterns.
     *
     * @param path    the path
     * @param patterns the patterns
     * @return the boolean if matches
     */
    private static boolean matchesAny(Path path, Set<PathMatcher> patterns)
    {
        for (PathMatcher pattern : patterns)
        {
            if (pattern.matches(path))
            {
                return true;
            }
        }
        return false;
    }

    private Set<OnFileChangeListener> matchedListeners(Path dir, Path file)
    {
        return pathSetConcurrentMap.get(dir)
                .stream()
                .filter(listener -> matchesAny(file, onFileChangeListenerSetConcurrentMap.get(listener)))
                .collect(Collectors.toSet());
    }

    private void notifyListeners(WatchKey key)
    {
        for (WatchEvent<?> event : key.pollEvents())
        {
            WatchEvent.Kind eventKind = event.kind();
            Path file = (Path) event.context();

            if (file != null)
            {
                String absoluteFilePath =  watchKeyPathConcurrentMap.get(key).toString() + File.separator + file.toString();
                if (eventKind.equals(ENTRY_CREATE))
                {
                    matchedListeners(watchKeyPathConcurrentMap.get(key), file).forEach(listener -> listener.onFileChange(absoluteFilePath));
                }
            }
        }
    }

    @Override
    public void run()
    {
        WatchKey key = watchService.poll();

        if (key == null)
        {
            LOGGER.info("No changes was registered in file");
            return;
        }

        if (watchKeyPathConcurrentMap.get(key) == null)
        {
            LOGGER.error("Watch key not recognized.");
            return;
        }

        notifyListeners(key);

        // Reset key to allow further events for this key to be processed.
        boolean valid = key.reset();
        if (!valid)
        {
            watchKeyPathConcurrentMap.remove(key);
        }
    }
}
