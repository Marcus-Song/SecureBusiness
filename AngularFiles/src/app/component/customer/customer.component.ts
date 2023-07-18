import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Observable, BehaviorSubject, map, startWith, catchError, of, switchMap, pipe } from 'rxjs';
import { CustomerStatus } from 'src/app/enum/customer-status';
import { DataState } from 'src/app/enum/datastate.enum';
import { ProfileState, CustomHttpResponse, Page, CustomerState } from 'src/app/interface/appstates';
import { User } from 'src/app/interface/user';
import { CustomerService } from 'src/app/service/customer.service';


@Component({
  selector: 'app-customer',
  templateUrl: './customer.component.html',
  styleUrls: ['./customer.component.css']
})
export class CustomerComponent implements OnInit {
  customerState$: Observable<ProfileState<CustomHttpResponse<CustomerState>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<CustomerState>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoadingSubject.asObservable();
  readonly DataState = DataState;
  readonly CustomerStatus = CustomerStatus;
  userService: any;

  constructor(private activatedRoute: ActivatedRoute, private customerService: CustomerService) { }

  ngOnInit(): void {
    console.log("Loading");
    this.customerState$ = this.activatedRoute.paramMap.pipe(
      switchMap((params: ParamMap) => {
        return this.customerService.customerDetail$(+params.get('id'))
      .pipe(
        map(response => {
          console.log(response);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADING }),
        catchError((error: string) => {
          return of({ dataState: DataState.LOADED, error })
        })
      )
      })
    );
  }

  updateCustomer(customerForm: NgForm): void {
    this.isLoadingSubject.next(true);
    this.customerState$ = this.customerService.update$(customerForm.value)
      .pipe(
        map(response => {
          console.log(response);
          this.dataSubject.next({ ...response, 
            data: {...response.data, 
              customer: {...response.data.customer, 
                // Add time in the imageUrl so that image will update every time
                imageUrl: `${response.data.user.imageUrl}?time=${new Date().getTime()}` }} });
          this.isLoadingSubject.next(false);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
        catchError((error: string) => {
          this.isLoadingSubject.next(false);
          return of({ dataState: DataState.LOADED, appData: this.dataSubject.value, error })
        })
      )
  }

  updateImage(image: File): void {
    if (image) {
      console.log("updating Image");
      this.isLoadingSubject.next(true);
      this.customerState$ = this.customerService.updateImage$(this.getFormData(image), this.dataSubject.value.data.customer.id)
        .pipe(
          map(response => {
            console.log(response);
            
            this.isLoadingSubject.next(false);
            return { dataState: DataState.LOADED, appData: this.dataSubject.value };
          }),
          startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
          catchError((error: string) => {
            this.isLoadingSubject.next(false);
            return of({ dataState: DataState.LOADED, appData: this.dataSubject.value, error })
          })
        )
    }
  }

  private getFormData(image: File): FormData {
    const formData = new FormData();
    formData.append("image", image);
    return formData;
  }
}
