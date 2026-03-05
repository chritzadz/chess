package com.project.chess.dto;


public class MoveRequest {
    private String from; // e.g., "e2"
    private String to;   // e.g., "e4"

    public MoveRequest() {}

    public MoveRequest(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
}
