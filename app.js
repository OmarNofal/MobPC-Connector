const express = require('express');
const statusRoutes = require('./routes/statusRoutes.js');
const browserRoutes = require('./routes/browserRoutes');
const clipboardRoutes = require('./routes/clipboardRoutes');
const directoryRoutes = require('./routes/directoryRoutes');
const osRoutes = require('./routes/osRoutes');
const fileOperationsRoutes = require('./routes/fileOperationsRoutes');

const {ErrorResponse, SuccessResponse} = require('./model/response');

const PORT = 6543

const app = express();

const router = express.Router();

app.use(express.json());
app.use(express.urlencoded({extended: true}))
app.use(directoryRoutes);
app.use(clipboardRoutes);
app.use(browserRoutes);
app.use(statusRoutes);
app.use(osRoutes);
app.use(fileOperationsRoutes);
app.use(router);

app.listen(PORT, () => {
    console.log("Server running")
})