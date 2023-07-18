import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { UserService } from '../service/user.service';


@Injectable({ providedIn: 'root' })
export class AuthenticationGuard {

  constructor(private userService: UserService, private router: Router) { }

    canActivate(routSnapShot: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        return this.isAuthenticated();
    }

    isAuthenticated(): boolean {
        if (this.userService.isAuthenticated()) {
            return true;
        } else {
            this.router.navigate(['/login']);
            return false;
        }
    }
}