import websocket

websocket.enableTrace(True)

URL = "wss://w3cp.web3-energy.com/w3cp"

print(f"Connecting to {URL} ...")

try:
    ws = websocket.create_connection(URL)
    print("CONNECTED")

    # Wait for server messages forever
    while True:
        msg = ws.recv()
        print(">>> FROM BACKEND:", msg)

except Exception as e:
    print("ERROR:", e)
