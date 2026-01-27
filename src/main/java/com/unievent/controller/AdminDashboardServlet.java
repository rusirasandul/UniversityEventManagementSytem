package com.unievent.controller;

import com.unievent.dao.AdminDAO;
import com.unievent.model.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private AdminDAO adminDAO = new AdminDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Security Check: Is user logged in?
        HttpSession session = request.getSession();
        if (session.getAttribute("admin_email") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 2. Fetch Data
        int[] stats = adminDAO.getSystemStats();
        List<Event> pendingEvents = adminDAO.getPendingEvents();

        // 3. Attach data to Request
        request.setAttribute("pendingCount", stats[0]);
        request.setAttribute("userCount", stats[1]);
        request.setAttribute("totalEventCount", stats[2]);
        request.setAttribute("pendingEventsList", pendingEvents);

        // 4. Forward to JSP
        request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);
    }
}