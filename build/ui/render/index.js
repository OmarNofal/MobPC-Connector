"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
document.getElementById('toggle-button')
    .addEventListener('click', () => {
    const f = window.toggleServer();
    console.log(f);
});
window.serverCallback((_, isOpen) => {
    console.log(isOpen);
    if (isOpen == 1) {
        document.getElementById('server-info').innerText = "ON";
    }
    else {
        document.getElementById('server-info').innerText = "OFF";
    }
});
