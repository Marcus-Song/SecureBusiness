import { Component } from '@angular/core';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  public x: number = 100;
  public y: number = 100;

  setPosition(x: number, y: number): void {
    this.x = x;
    this.y = y;
    console.log(this.x, this.y); 
  }
  
  moveHorizontal(distance: number): void {
    this.x += distance;
    console.log(this.x); 
  }

  moveVerical(distance: number): void { 
    this.y += distance;
    console.log(this.y); 
  }
}
