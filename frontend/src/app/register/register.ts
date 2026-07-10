import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Auth } from '../services/auth';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private auth = inject(Auth);

  email = '';
  password = '';
  firstName = '';
  lastName = '';

  register() {
    this.auth.register(this.email, this.password, this.firstName, this.lastName).subscribe({
      next: (response) => console.log('Register success:', response),
      error: (err) => console.error('Register failed:', err),
    });
  }
}
