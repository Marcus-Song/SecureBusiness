import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Observable, BehaviorSubject, map, startWith, catchError, of } from 'rxjs';
import { CustomerStatus } from 'src/app/enum/customer-status';
import { DataState } from 'src/app/enum/datastate.enum';
import { ProfileState, CustomHttpResponse, Page } from 'src/app/interface/appstates';
import { Customer } from 'src/app/interface/customer';
import { User } from 'src/app/interface/user';
import { CustomerService } from 'src/app/service/customer.service';

@Component({
  selector: 'app-newcustomer',
  templateUrl: './newcustomer.component.html',
  styleUrls: ['./newcustomer.component.css']
})
export class NewcustomerComponent implements OnInit {
  newCustomerState$: Observable<ProfileState<CustomHttpResponse<Page<Customer> & User>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Page<Customer> & User>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoadingSubject.asObservable();
  readonly DataState = DataState;
  readonly CustomerStatus = CustomerStatus;

  constructor(private customerServie: CustomerService) { }

  ngOnInit(): void {
    console.log("Loading");
    this.newCustomerState$ = this.customerServie.customer$()
      .pipe(
        map(response => {
          console.log(response);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADING }),
        catchError((error: string) => {
          return of({ dataState: DataState.ERROR, error })
        })
      )
  }

  createCustomer(newCustomerForm: NgForm): void {
    this.isLoadingSubject.next(true);
    console.log("Loading");
    this.newCustomerState$ = this.customerServie.newCustomer$(newCustomerForm.value)
      .pipe(
        map(response => {
          console.log(response);
          newCustomerForm.reset({ type: "INDIVIDUAL", status: "ACTIVE" });
          this.dataSubject.next(this.dataSubject.value);
          this.isLoadingSubject.next(false);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
        catchError((error: string) => {
          this.isLoadingSubject.next(false);
          return of({ dataState: DataState.LOADED, error })
        })
      )
  }

}
