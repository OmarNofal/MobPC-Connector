import '@fontsource-variable/inter/wght.css'
import './static/index.css'

import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './screens/app'
import { HashRouter } from 'react-router-dom'



const container = document.getElementById('container')

const root = createRoot(container)




root.render(
    <HashRouter basename="/">
        <React.StrictMode>
            <App />
        </React.StrictMode>
    </HashRouter>
)