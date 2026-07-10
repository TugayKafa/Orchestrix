import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { Auth } from './services/auth';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  protected auth = inject(Auth);

  logout() {
    this.auth.logout().subscribe({
      next: () => console.log('Logged out'),
      error: (err) => console.error('Logout failed:', err),
    });
  }
}
