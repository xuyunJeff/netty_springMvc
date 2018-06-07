var ws = new WebSocket("ws://localhost:7878/socket");

ws.onopen = function(evt) {
    console.log("Connection open ...");
    //[send] {'SToken':'123321','token':'localhost','msg':'歡迎來到銀河系'}
    ws.send("[login] {'SToken':'123321','token':'localhost'}");
};

ws.onmessage = function(evt) {
    console.log("Received Message: " + evt.data);
};

ws.onclose = function(evt) {
    console.log("Connection closed.");
};
