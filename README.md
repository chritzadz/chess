hmmm just a chess engine so that I can apply/train my chess bot.

games  connected via db, create a random generatede game_id, other player will join if game exists.

joined player will always be Black, and the one created in white.

normal board given in FEN string so it is always in the perspective of White. So, flips should be handled in frontend.

Websocket url: https://domain/games?id=1234556

websocket connection always send game state in perspective of player 1,
send move by sending message ToFrom (e.g. e2e4). Will defnitely do a proper chess notation like e2,Bd6,Qxa5 (but for now im too lazy about it so ToFrom format works...)

enjoy hehe