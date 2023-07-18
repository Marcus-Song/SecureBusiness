import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, catchError, of, switchMap, tap, throwError } from 'rxjs';
import { HttpCacheService } from '../service/http.cache.service';

@Injectable()
export class CacheInterceptor implements HttpInterceptor {

  constructor(private httpCache: HttpCacheService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> | Observable<HttpResponse<unknown>> {
    // WHITE LIST URLs
    if (request.url.includes('/verify') || request.url.includes('/login') || request.url.includes('/register')
      || request.url.includes('/refresh') || request.url.includes('/resetpassowrd')) {
      return next.handle(request);
    }

    if (request.url.includes('download') || request.method !== 'GET') {
      this.httpCache.evictAll();
    }

    const cachedResponse: HttpResponse<any> = this.httpCache.get(request.url);
    if (cachedResponse) {
      console.log("Found response from cache: ", cachedResponse);
      this.httpCache.logCache();
      return of(cachedResponse);
    }
    return this.handleRequestCahce(request, next);
  }

  private handleRequestCahce(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      tap(response => {
        if (response instanceof HttpResponse && request.method !== 'DELETE') {
          console.log("Saving cached response: ", response);
          this.httpCache.put(request.url, response);
        }
      })
    );
  }
}
