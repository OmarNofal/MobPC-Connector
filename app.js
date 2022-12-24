const express = require('express');
const statusRoutes = require('./routes/statusRoutes.js');
const browserRoutes = require('./routes/browserRoutes');

const {ErrorResponse, SuccessResponse} = require('./model/response');

const PORT = 6543

const app = express();

const router = express.Router();

app.use(express.json());
app.use(express.urlencoded({extended: true}))
app.use(browserRoutes);
app.use(statusRoutes);
app.use(router);

app.listen(PORT, () => {
    console.log("Server running")
})