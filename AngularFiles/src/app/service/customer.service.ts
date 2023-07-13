import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { CustomHttpResponse, CustomerState, Page, Profile } from '../interface/appstates';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { User } from '../interface/user';
import { Stats } from '../interface/stats';
import { Customer } from '../interface/customer';
import { Invoice } from '../interface/invoice';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private readonly server: string = '/api';

  constructor(private http: HttpClient) { }


  customer$ = (page: number = 0) => <Observable<CustomHttpResponse<Page<Customer> & User & Stats>>>
    this.http.get<CustomHttpResponse<Page<Customer> & User & Stats>>(`${this.server}/customer/list?page=${page}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  customerDetail$ = (customerId: number) => <Observable<CustomHttpResponse<CustomerState>>>
    this.http.get<CustomHttpResponse<CustomerState>>(`${this.server}/customer/get/${customerId}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  newCustomer$ = (customer: Customer) => <Observable<CustomHttpResponse<Customer & User>>>
    this.http.post<CustomHttpResponse<Customer & User>>(`${this.server}/customer/create`, customer)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  searchCustomer$ = (name: string = '', page: number = 0) => <Observable<CustomHttpResponse<Page<Customer> & User>>>
    this.http.get<CustomHttpResponse<Page<Customer> & User>>
      (`${this.server}/customer/search?name=${name}&page=${page}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  update$ = (customer: Customer) => <Observable<CustomHttpResponse<CustomerState>>>
    this.http.post<CustomHttpResponse<CustomerState>>(`${this.server}/customer/update`, customer)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  newInvoice$ = () => <Observable<CustomHttpResponse<Customer[] & User>>>
    this.http.get<CustomHttpResponse<Customer[] & User>>(`${this.server}/customer/invoice/new`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  createInvoice$ = (customerId: number, invoice: Invoice) => <Observable<CustomHttpResponse<Customer[] & User>>>
    this.http.post<CustomHttpResponse<Customer[] & User>>
      (`${this.server}/customer/invoice/add-to-customer/${customerId}`, invoice)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  invoices$ = (page: number = 0) => <Observable<CustomHttpResponse<Page<Invoice> & User>>>
    this.http.get<CustomHttpResponse<Page<Invoice> & User>>(`${this.server}/customer/invoice/list?page=${page}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  invoice$ = (invoiceId: number) => <Observable<CustomHttpResponse<Customer & User & Invoice>>>
    this.http.get<CustomHttpResponse<Customer & User & Invoice>>(`${this.server}/customer/invoice/get/${invoiceId}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  public handleError(httpError: HttpErrorResponse): Observable<never> {
    let errorMessage: string;
    if (httpError.error instanceof ErrorEvent) {
      errorMessage = `A client error occurred - ${httpError.error.message}`;
    } else if (httpError.error.reason) {
      errorMessage = httpError.error.reason;
    } else {
      errorMessage = `An error occurred - Error status: ${httpError.error.status}`;
    }
    return throwError(() => errorMessage);
  }
}