package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;

/**
 * Interface for updating links in the system. Implementations should handle the actual update logic for different types
 * of links.
 */
public interface LinkUpdater {
    /**
     * Updates a link and returns whether the update was successful.
     *
     * @param link The link to update
     * @return true if the update was successful, false otherwise
     */
    boolean update(Link link);
}
