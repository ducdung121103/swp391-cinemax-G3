package services;

import java.util.List;
import models.CinemaBranch;
import repositories.CinemaBranches;

public class HomeService {

    private final CinemaBranches branchDao = new CinemaBranches();

    /**
     * Get all active cinema branches for public display
     * @return List of active branches sorted by name
     */
    public List<CinemaBranch> getActiveBranches() {
        return branchDao.getActiveBranches();
    }
}