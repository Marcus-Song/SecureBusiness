import { HttpEvent, HttpEventType } from '@angular/common/http';
import { Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, map, startWith, catchError, of } from 'rxjs';
import { CustomerStatus } from 'src/app/enum/customer-status';
import { DataState } from 'src/app/enum/datastate.enum';
import { ProfileState, CustomHttpResponse, Page } from 'src/app/interface/appstates';
import { Customer } from 'src/app/interface/customer';
import { User } from 'src/app/interface/user';
import { CustomerService } from 'src/app/service/customer.service';
import { UserService } from 'src/app/service/user.service';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-customers',
  templateUrl: './customers.component.html',
  styleUrls: ['./customers.component.css']
})
export class CustomersComponent {
  customerState$: Observable<ProfileState<CustomHttpResponse<Page<Customer> & User>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Page<Customer> & User>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoadingSubject.asObservable();
  private showLogSubject = new BehaviorSubject<boolean>(false);
  showLogs$ = this.showLogSubject.asObservable();
  private currentPageSubject = new BehaviorSubject<number>(0);
  currentPage$ = this.currentPageSubject.asObservable();
  private fileStatusSubject = new BehaviorSubject<{ status:string, type:string, percent:number }>(undefined);
  fileStatus$ = this.fileStatusSubject.asObservable();
  readonly DataState = DataState;
  readonly CustomerStatus = CustomerStatus;

  constructor(private router: Router ,private userService: UserService, private customerService: CustomerService) { }

  ngOnInit(): void {
    console.log("Loading");
    this.customerState$ = this.customerService.searchCustomer$()
      .pipe(
        map(response => {
          console.log(response);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADING }),
        catchError((error: string) => {
          return of({ dataState: DataState.ERROR, error, appData: this.dataSubject.value })
        })
      )
  }

  searchCustomer(searchForm: NgForm): void {
    this.currentPageSubject.next(0);
    console.log(searchForm.value);
    this.customerState$ = this.customerService.searchCustomer$(searchForm.value.customerName)
      .pipe(
        map(response => {
          console.log(response);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value   }),
        catchError((error: string) => {
          return of({ dataState: DataState.ERROR, error, appData: this.dataSubject.value })
        })
      )
  }

  goToPage(pageNumber?: number, name?: string): void {
    this.customerState$ = this.customerService.searchCustomer$(name, pageNumber)
      .pipe(
        map(response => {
          console.log(response);
          this.dataSubject.next(response);
          this.currentPageSubject.next(pageNumber);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADING, appData: this.dataSubject.value }),
        catchError((error: string) => {
          return of({ dataState: DataState.ERROR, error, appData: this.dataSubject.value })
        })
      )
  }

  selectCustomer(customer: Customer): void {
    this.router.navigate([`/customers/${customer.id}`]);
  }

  report(): void {
    this.customerState$ = this.customerService.downloadReport$()
      .pipe(
        map(response => {
          console.log(response);
          this.reportProgress(response);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
        startWith({ dataState: DataState.LOADING, appData: this.dataSubject.value }),
        catchError((error: string) => {
          return of({ dataState: DataState.ERROR, error, appData: this.dataSubject.value })
        })
      )
  }

  private reportProgress(httpEvent: HttpEvent<string[] | Blob>): void {
    switch (httpEvent.type) {
      case HttpEventType.DownloadProgress || HttpEventType.UploadProgress:
        // Report progress
        this.fileStatusSubject.next({ status: 'progress', type: 'downloading...', percent: Math.round(100 * httpEvent.loaded / httpEvent.total)});
        break;
      case HttpEventType.ResponseHeader:
        console.log("Got response header", httpEvent);
        // Report progress
        break;
      case HttpEventType.Response:
        // Report progress
        saveAs(new File([<Blob>httpEvent.body], httpEvent.headers.get('File-Name'), {type: `${httpEvent.headers.get('Content-Type')};charset=utf-8`}));
        this.fileStatusSubject.next(undefined);
        break;
      default:
        console.log(httpEvent)
        break;
    }
  }
}
