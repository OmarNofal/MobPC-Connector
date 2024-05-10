import { BehaviorSubject } from "rxjs";



export function mapBehaviorSubject<T, Y>(subject: BehaviorSubject<T>, mapFn: (arg0: T) => Y) {

    const newSubject = new BehaviorSubject<Y>(mapFn(subject.value)) 
    subject.subscribe(v => newSubject.next(mapFn(v)))

    return newSubject
}