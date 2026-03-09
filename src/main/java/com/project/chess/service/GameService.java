package com.project.chess.service;

import com.project.chess.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {
    private GameEngine engine;

    public GameService() {
        engine = new GameEngine();
    }

    public void resetGame() {
        engine.reset();
    }

    public void resetGame(String colorStr) {
        Color color = "BLACK".equalsIgnoreCase(colorStr) ? Color.BLACK : Color.WHITE;
        engine.reset(color);
    }

    public GameState getGameState(String gameId) {
        GameState state = new GameState();
        
        List<GameState.PieceDTO> pieceDTOs = new ArrayList<>();
        for (Piece p : engine.getPieces()) {
            pieceDTOs.add(new GameState.PieceDTO(
                p.getType(),
                p.getPieceColor().name(),
                p.getPosition().name(),
                p.getImagePath()
            ));
        }
        state.setPieces(pieceDTOs);
        state.setWhiteToMove(engine.isWhiteToMove());
        state.setGameOver(engine.isGameOver());
        state.setWinner(engine.getWinner());
        state.setPlayerColor(engine.getPlayerColor().name());
        
        Color currentColor = engine.isWhiteToMove() ? Color.WHITE : Color.BLACK;
        state.setInCheck(engine.isInCheck(currentColor));
        state.setLegalMoves(calculateAllLegalMoves(currentColor));
        
        return state;
    }

    public boolean makeMove(String from, String to) {
        try {
            Position fromPos = Position.valueOf(from);
            Position toPos = Position.valueOf(to);
            return engine.move(fromPos, toPos);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Map<String, List<String>> calculateAllLegalMoves(Color color) {
        Map<String, List<String>> allMoves = new HashMap<>();
        for (Piece p : engine.getPieces()) {
            if (p.getPieceColor() == color) {
                List<Position> positions = engine.getLegalMovesFor(p);
                if (!positions.isEmpty()) {
                    List<String> moves = new ArrayList<>();
                    for (Position pos : positions) {
                        moves.add(pos.name());
                    }
                    allMoves.put(p.getPosition().name(), moves);
                }
            }
        }
        return allMoves;
    }
}
