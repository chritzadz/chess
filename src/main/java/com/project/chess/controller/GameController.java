package com.project.chess.controller;


import com.project.chess.dto.MoveRequest;
import com.project.chess.model.GameState;
import com.project.chess.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/state")
    public GameState getGameState() {
        return gameService.getGameState();
    }

    @PostMapping("/move")
    public GameState makeMove(@RequestBody MoveRequest request) {
        gameService.makeMove(request.getFrom(), request.getTo());
        return gameService.getGameState();
    }

    @PostMapping("/reset")
    public GameState resetGame(@RequestParam(value = "color", defaultValue = "WHITE") String color) {
        gameService.resetGame(color);
        return gameService.getGameState();
    }
}
