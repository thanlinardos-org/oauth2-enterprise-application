const fs = require('node:fs');
const https = require('node:https');

process.loadEnvFile();

// Create an HTTPS agent that presents the client certificate for mTLS
const agent = new https.Agent({
    cert: fs.readFileSync('src/assets/ssl/local_angular_ui_client.crt', 'utf-8'),
    key: fs.readFileSync('src/assets/ssl/local_angular_ui_client.key', 'utf-8'),
    ca: [fs.readFileSync('src/assets/ssl/intermediate.crt', 'utf-8')],
    rejectUnauthorized: false
});

const PROXY_CONFIG = {
    "/api/**": {
        "target": process.env.ROOT_URL,
        "secure": false,
        "changeOrigin": true,
        "logLevel": "debug",
        "pathRewrite": {
            "^/api": ""
        },
        "agent": agent
    }
}

module.exports = PROXY_CONFIG;
