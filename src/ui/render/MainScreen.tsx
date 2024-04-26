import { BehaviorSubject, Observable } from "rxjs";
import { MainScreenViewModel } from "./MainScreenViewModel";
import { useEffect, useState } from "react";
import { Button, CircularProgress, LinearProgress } from "@mui/material";






export default function MainScreen(props: {vm: MainScreenViewModel }) {

    const vm = props.vm
    const state = useUnwrap(vm.state)

    return (
        <div>

            {
                (state == 'loading' || state.isMainServerOpen == true) &&
                <CircularProgress variant="indeterminate"></CircularProgress>
            }
        
            <Button 
            onClick={vm.stop}
            variant= 'contained'
            >
                {state == 'loading' ? 'Stop' : 'Start'} 
            </Button>

            <p>{ state == 'loading' ? "Loading" : "Not loading"}</p>

        </div>
    )

}



function get<T>(observable$: Observable<T>): T {
    let value;
    observable$.subscribe((val) => (value = val)).unsubscribe();
    return value;
}

// Custom React hook for unwrapping observables
function useUnwrap<T>(observable$: BehaviorSubject<T>): T {
    const [value, setValue] = useState(() => get(observable$));
  
    useEffect(() => {
      const subscription = observable$.subscribe(setValue);
      return function cleanup() {
        subscription.unsubscribe();
      };
    }, [observable$]);
  
    return value;
  }