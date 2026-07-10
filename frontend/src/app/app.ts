import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Auth } from './services/auth';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  private auth = inject(Auth);

  logout() {
    this.auth.logout().subscribe({
      next: () => console.log('Logged out'),
      error: (err) => console.error('Logout failed:', err),
    });
  }
}
