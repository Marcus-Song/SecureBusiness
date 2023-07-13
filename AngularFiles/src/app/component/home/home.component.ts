import { Component, OnInit } from '@angular/core';
import { EventType, Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, map, of, startWith } from 'rxjs';
import { CustomerStatus } from 'src/app/enum/customer-status';
import { DataState } from 'src/app/enum/datastate.enum';
import { CustomHttpResponse, Page, Profile, ProfileState } from 'src/app/interface/appstates';
import { Customer } from 'src/app/interface/customer';
import { User } from 'src/app/interface/user';
import { CustomerService } from 'src/app/service/customer.service';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  homeState$: Observable<ProfileState<CustomHttpResponse<Page<Customer> & User>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Page<Customer> & User>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoadingSubject.asObservable();
  private showLogSubject = new BehaviorSubject<boolean>(false);
  showLogs$ = this.showLogSubject.asObservable();
  private currentPageSubject = new BehaviorSubject<number>(0);
  currentPage$ = this.currentPageSubject.asObservable();
  readonly DataState = DataState;
  readonly CustomerStatus = CustomerStatus;

  constructor(private router: Router, private userService: UserService, private customerServie: CustomerService) { }

  ngOnInit(): void {
    console.log("Loading");
    this.homeState$ = this.customerServie.customer$()
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

  goToPage(pageNumber?: number): void {
    this.homeState$ = this.customerServie.customer$(pageNumber)
      .pipe(
        map(response => {
          console.log(response);
          this.dataSubject.next(response);
          this.currentPageSubject.next(pageNumber);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADING, appData: this.dataSubject.value }),
        catchError((error: string) => {
          return of({ dataState: DataState.ERROR, error })
        })
      )
  }

  selectCustomer(customer: Customer): void {
    this.router.navigate([`/customers/${customer.id}`]);
  }
}
