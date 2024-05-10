import '@fontsource-variable/inter/wght.css'
import './static/index.css'

import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './screens/app'
import { HashRouter } from 'react-router-dom'



const body = document.body

const root = createRoot(body)




root.render(
    <HashRouter basename="/">
        <React.StrictMode>
            <App />
        </React.StrictMode>
    </HashRouter>
)