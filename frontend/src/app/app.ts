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
  protected theme = signal(localStorage.getItem('orchestrix_theme') || 'dark');

  constructor() {
    document.documentElement.setAttribute('data-theme', this.theme());
  }

  toggleTheme() {
    const next = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(next);
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('orchestrix_theme', next);
  }

  logout() {
    this.auth.logout().subscribe({
      next: () => console.log('Logged out'),
      error: (err) => console.error('Logout failed:', err),
    });
  }
}
