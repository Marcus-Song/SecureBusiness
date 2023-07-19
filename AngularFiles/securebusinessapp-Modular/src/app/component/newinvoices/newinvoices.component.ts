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
  selector: 'app-newinvoices',
  templateUrl: './newinvoices.component.html',
  styleUrls: ['./newinvoices.component.css']
})
export class NewinvoicesComponent implements OnInit {
  newInvoiceState$: Observable<ProfileState<CustomHttpResponse<Customer[] & User>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Customer[] & User>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoadingSubject.asObservable();
  readonly DataState = DataState;
  readonly CustomerStatus = CustomerStatus;
  serviceBoxes: any[] = [];

  constructor(private customerServie: CustomerService) { }

  ngOnInit(): void {
    console.log("Loading");
    this.newInvoiceState$ = this.customerServie.newInvoice$()
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

  newInvoice(newInvoiceForm: NgForm): void {
    this.dataSubject.next({...this.dataSubject.value, message: null});
    this.isLoadingSubject.next(true);
    newInvoiceForm.value.services = this.serviceBoxes.map(box => box.service)
    newInvoiceForm.value.price = this.serviceBoxes.map(box => box.price)
    newInvoiceForm.value.total = newInvoiceForm.value.price.reduce((sum, price) => sum + price, 0);
    this.newInvoiceState$ = this.customerServie.createInvoice$(newInvoiceForm.value.customerId, newInvoiceForm.value)
      .pipe(
        map(response => {
          console.log(response);
          newInvoiceForm.reset({ status: "PENDING" });
          this.dataSubject.next(response);
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

  addBox() {
    this.serviceBoxes.push({ service: '', price: '' });
  }

  removeBox(index: number) {
    this.serviceBoxes.splice(index, 1);
  }

  submitForm(form: NgForm) {
    if (form.valid) {
      const services = this.serviceBoxes.map(box => box.service);
      const price = this.serviceBoxes.map(box => box.price);
      console.log(services, price);
      // Here, you can perform further actions with the 'services' array
    }
  }


}
