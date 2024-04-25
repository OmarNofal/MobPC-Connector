
const { createRoot } = require('react-dom/client')




const body = document.body

const root = createRoot(body)




function MyBasicComponent() {

    return (
        <a href='https://reactjs.org'>Hello from React!</a>
    )
}

root.render(<MyBasicComponent/>)