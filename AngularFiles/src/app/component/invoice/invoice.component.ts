import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Observable, BehaviorSubject, map, startWith, catchError, of, switchMap } from 'rxjs';
import { DataState } from 'src/app/enum/datastate.enum';
import { ProfileState, CustomHttpResponse } from 'src/app/interface/appstates';
import { Customer } from 'src/app/interface/customer';
import { Invoice } from 'src/app/interface/invoice';
import { User } from 'src/app/interface/user';
import { CustomerService } from 'src/app/service/customer.service';
import { jsPDF as pdf } from 'jspdf';

@Component({
  selector: 'app-invoice',
  templateUrl: './invoice.component.html',
  styleUrls: ['./invoice.component.css']
})
export class InvoiceComponent implements OnInit {
  invoiceState$: Observable<ProfileState<CustomHttpResponse<Customer & Invoice & User>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Customer & Invoice & User>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoadingSubject.asObservable();
  readonly DataState = DataState;

  constructor(private activatedRoute: ActivatedRoute, private customerService: CustomerService) { }

  ngOnInit(): void {
    this.invoiceState$ = this.activatedRoute.paramMap.pipe(
      switchMap((params: ParamMap) => {
        return this.customerService.invoice$(+params.get('id'))
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
      })
    );
  }

  exportAsPDF(): void {
    const fileName = `invoice-${this.dataSubject.value.data['invoice']['invoiceNumber']}.pdf`;
    const doc = new pdf();
    doc.html(document.getElementById('invoice'), { margin: 5, windowWidth: 1000, width: 200 , callback: (invoice) => {invoice.save(fileName);}});
  }

}
