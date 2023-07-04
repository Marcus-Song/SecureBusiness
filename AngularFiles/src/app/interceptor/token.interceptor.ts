import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, catchError, switchMap, throwError } from 'rxjs';
import { Key } from '../enum/key.enum';
import { UserService } from '../service/user.service';
import { CustomHttpResponse, Profile } from '../interface/appstates';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  private isTokenRefeshing: boolean = false;
  private refreshTokenSubject: BehaviorSubject<CustomHttpResponse<Profile>> = new BehaviorSubject(null); 

  constructor(private userService: UserService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> | Observable<HttpResponse<unknown>> {
    // WHITE LIST URLs
    if (request.url.includes('/verify') || request.url.includes('/login') || request.url.includes('/register') 
    || request.url.includes('/refresh') || request.url.includes('/resetpassowrd')) {
      return next.handle(request);
    }
    return next.handle(this.addAuthorizationTokenHeader(request, localStorage.getItem(Key.TOKEN)))
    .pipe(catchError((httpError: HttpErrorResponse) => {
      if (httpError instanceof HttpErrorResponse && httpError.status === 401 /*&& httpError.error.reason.includes('expired')*/) {
        return this.handleRefreshToken(request, next);
      } else {
        return throwError( () => httpError);
      }
    }));
  }

  private handleRefreshToken(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if(!this.isTokenRefeshing) {
      //console.log("Refreshing token...");
      this.isTokenRefeshing = true;
      this.refreshTokenSubject.next(null);
      return this.userService.refreshToken$().pipe(
        switchMap((response) => {
          //console.log("Token refresh response:", response);
          this.isTokenRefeshing = false;
          this.refreshTokenSubject.next(response);
          //console.log("New Token:", response.data.access_token);
          //console.log("Sending original requess", request);
          return next.handle(this.addAuthorizationTokenHeader(request, response.data.access_token));
        })
      );
    } else {
      this.refreshTokenSubject.pipe(
        switchMap((response) => {
          return next.handle(this.addAuthorizationTokenHeader(request, response.data.access_token));
        })
      )
    }
  }

  private addAuthorizationTokenHeader(request: HttpRequest<unknown>, token: string): HttpRequest<any> {
    return request.clone({setHeaders: {'Authorization': `Bearer ${token}`}});
  }
}
