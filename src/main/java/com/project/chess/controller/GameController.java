package com.project.chess.controller;


import com.project.chess.dto.MoveRequest;
import com.project.chess.entity.Games;
import com.project.chess.model.GameState;
import com.project.chess.repository.GamesRepository;
import com.project.chess.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    @Autowired
    private GamesRepository gamesRepository;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/id")
    public ResponseEntity<?> getGame(@RequestParam String id) {
        if (!gamesRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        Games game = gamesRepository.findById(id).orElse(null);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/create")
    public ResponseEntity<?> postGame() {
        Long id = gamesRepository.save(new Games()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
}

