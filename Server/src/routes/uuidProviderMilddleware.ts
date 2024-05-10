import { Request, Response } from 'express';
import { ErrorResponse } from '../model/response';



export default function createUuidProviderFunction(
  isLoggedIn: (token: string) => boolean,
) {

  return (req: Request, res: Response, next: () => void) => {
    const authHeader = req.headers['authorization'];
  
    if (!authHeader) {
      return res.status(401).json({ error: 'Authorization header missing' });
    }
  
    // The Authorization header usually looks like: "Bearer <token>"
    const token = authHeader.split(' ')[1]; // Get the token part after 'Bearer'
  
    if (!token) {
      return res.status(401).json({ error: 'No token provided' });
    }
  
    if (isLoggedIn(token)) {
        next();
    } else {
      return res.status(401).json(new ErrorResponse(34, "Expired token"))
    }
  }

}