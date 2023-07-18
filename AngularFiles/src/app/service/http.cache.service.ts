import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class HttpCacheService {
  private httpResponseCache: { [key: string]: HttpResponse<any> } = {};

  put = (key: string, httpResponse: HttpResponse<any>) => {
    console.log("Caching response: ", httpResponse);
    this.httpResponseCache[key] = httpResponse;
  }

  get = (key: string): HttpResponse<any>| null | undefined => this.httpResponseCache[key];

  evict = (key: string): void => {
    console.log("Deleting cache with key: ", key);
    this.httpResponseCache[key] = null;
  }

  evictAll = (): void => {
    console.log("Deleting all caches");
    this.httpResponseCache = {};
  }

  logCache = (): void => console.log(this.httpResponseCache);
}