
import { createRoot } from 'react-dom/client'
import MainScreen from './MainScreen'
import { MainScreenViewModel } from './MainScreenViewModel'

const vm = new MainScreenViewModel()

const body = document.body

const root = createRoot(body)

root.render(<MainScreen vm={vm} onToggle={undefined}/>)