import asyncio
import websockets
import sys

async def send_moves(websocket, username):
    # Send user ID as first message
    await websocket.send(f"USER:{username}")
    
    while True:
        move = await asyncio.get_event_loop().run_in_executor(None, input, "Enter your move (e.g. e2e4): ")
        await websocket.send(move)

async def receive_messages(websocket):
    while True:
        response = await websocket.recv()
        print(f"Received: {response}")

async def connect():
    uri = "ws://localhost:8080/game?id=3"
    username = input("Enter your username: ")
    async with websockets.connect(uri) as websocket:
        await asyncio.gather(
            send_moves(websocket, username),
            receive_messages(websocket)
        )

asyncio.run(connect())