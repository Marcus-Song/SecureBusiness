import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule }   from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './component/login/login.component';
import { RegisterComponent } from './component/register/register.component';
import { VerifyComponent } from './component/verify/verify.component';
import { ResetpasswordComponent } from './component/resetpassword/resetpassword.component';
import { CustomerComponent } from './component/customer/customer.component';
import { ProfileComponent } from './component/profile/profile.component';
import { HomeComponent } from './component/home/home.component';
import { NavbarComponent } from './component/navbar/navbar.component';
import { StatsComponent } from './component/stats/stats.component';
import { TokenInterceptor } from './interceptor/token.interceptor';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NewcustomerComponent } from './component/newcustomer/newcustomer.component';
import { CustomersComponent } from './component/customers/customers.component';
import { InvoiceComponent } from './component/invoice/invoice.component';
import { InvoicesComponent } from './component/invoices/invoices.component';
import { NewinvoicesComponent } from './component/newinvoices/newinvoices.component';
import { CacheInterceptor } from './interceptor/cache.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    VerifyComponent,
    ResetpasswordComponent,
    CustomerComponent,
    ProfileComponent,
    HomeComponent,
    NavbarComponent,
    StatsComponent,
    NewcustomerComponent,
    CustomersComponent,
    InvoiceComponent,
    InvoicesComponent,
    NewinvoicesComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,      //<----------make sure you have added this.
    DragDropModule, 
    BrowserAnimationsModule,
  ],
  providers: [{ provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
              { provide: HTTP_INTERCEPTORS, useClass: CacheInterceptor, multi: true },
             ],
  bootstrap: [AppComponent]
})
export class AppModule { }
