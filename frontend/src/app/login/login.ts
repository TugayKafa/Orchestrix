import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Auth } from '../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private auth = inject(Auth);
  email = '';
  password = '';

  login() {
    this.auth.login(this.email, this.password).subscribe({
      next: (response) => console.log('Login success:', response),
      error: (err) => console.error('Login failed:', err),
    });
  }
}
