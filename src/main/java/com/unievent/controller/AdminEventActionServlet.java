package com.unievent.controller;

import com.unievent.dao.AdminDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/eventAction")
public class AdminEventActionServlet extends HttpServlet {

    private AdminDAO adminDAO = new AdminDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int eventId = Integer.parseInt(request.getParameter("event_id"));
            String action = request.getParameter("action"); // 'approve' or 'reject'

            String newStatus = action.equalsIgnoreCase("approve") ? "APPROVED" : "REJECTED";

            // Call the thread-safe method
            String result = adminDAO.updateEventStatus(eventId, newStatus);

            if (result.equals("SUCCESS")) {
                request.getSession().setAttribute("msg", "Event " + newStatus + " successfully.");
            } else if (result.equals("ALREADY_HANDLED")) {
                // Alert the admin that someone else beat them to it!
                request.getSession().setAttribute("error", "⚠️ Alert: Another admin has already processed this event!");
            } else {
                request.getSession().setAttribute("error", "System Error: Could not update database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "System Error: " + e.getMessage());
        }

        // Redirect back to dashboard to refresh the list
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}