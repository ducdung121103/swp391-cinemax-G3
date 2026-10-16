package services;

import com.google.gson.Gson;
import repositories.DashboardRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardService {

    private DashboardRepository dashboardRepo = new DashboardRepository();
    private Gson gson = new Gson();

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dto = new HashMap<>();

        // 1. KPIs
        Map<String, Object> kpi = new HashMap<>();

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        LocalDate lastMonthDate = today.minusMonths(1);
        int lastMonth = lastMonthDate.getMonthValue();
        int lastMonthYear = lastMonthDate.getYear();

        // Revenue (Month)
        Double revenueThisMonth = dashboardRepo.getTotalRevenueForMonth(currentMonth, currentYear);
        Double revenueLastMonth = dashboardRepo.getTotalRevenueForMonth(lastMonth, lastMonthYear);
        kpi.put("totalRevenueMonth", revenueThisMonth);

        if (revenueLastMonth > 0) {
            Double change = ((revenueThisMonth - revenueLastMonth) / revenueLastMonth) * 100;
            kpi.put("revenueChange", change);
            kpi.put("revenueIncreased", change >= 0);
        } else {
            kpi.put("revenueChange", 0.0);
            kpi.put("revenueIncreased", true);
        }

        int ticketsThisMonth = dashboardRepo.getTicketsSoldForMonth(currentMonth, currentYear);
        int ticketsLastMonth = dashboardRepo.getTicketsSoldForMonth(lastMonth, lastMonthYear);
        kpi.put("ticketsSoldMonth", ticketsThisMonth);

        int ticketDiff = ticketsThisMonth - ticketsLastMonth;
        kpi.put("ticketsChange", ticketDiff);

        // New Customers (Month)
        kpi.put("newCustomersMonth", dashboardRepo.getNewCustomersCount(currentMonth, currentYear));

        // Active Branches
        kpi.put("activeBranches", dashboardRepo.getActiveBranchesCount());
        kpi.put("totalBranches", dashboardRepo.getTotalBranchesCount());

        dto.put("kpi", kpi);

        // 2. Charts
        dto.put("revenueByBranch", dashboardRepo.getRevenueByBranch());
        dto.put("topMovies", dashboardRepo.getTopMovies(5));
        dto.put("revenueTrend", dashboardRepo.getRevenueTrend(6));

        // 3. Transactions
        dto.put("recentTransactions", dashboardRepo.getRecentTransactions(10));

        // 4. Movie Status
        dto.put("movieStatus", dashboardRepo.getMovieStatus());

        return dto;
    }

    public String toJson(Object object) {
        return gson.toJson(object);
    }
}
