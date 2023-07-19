import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { User } from 'src/app/interface/user';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  @Input() user: User;

  constructor(private userService: UserService, private router: Router) { }
  
  logOut(): void {
    this.userService.logOut();
    this.router.navigate(['/login']);
  }
}
