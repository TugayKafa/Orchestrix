import { Service, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

interface AuthResponse {
  email: string;
  refreshToken: string;
  accessToken: string;
}

interface AccessTokenResponse {
  accessToken: string;
}

@Service()
export class Auth {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  loggedIn = signal(!!this.getAccessToken());

  register(email: string, password: string, firstName: string, lastName: string) {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, { email, password, firstName, lastName })
      .pipe(tap((response) => this.storeTokens(response.accessToken, response.refreshToken)));
  }

  login(email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap((response) => this.storeTokens(response.accessToken, response.refreshToken)));
  }

  refresh(token: string) {
    return this.http.post<AccessTokenResponse>(`${this.apiUrl}/refresh`, { token });
  }

  logout() {
    return this.http
      .post<void>(`${this.apiUrl}/logout`, { token: this.getRefreshToken() })
      .pipe(tap(() => this.clearTokens()));
  }

  storeTokens(accessToken: string, refreshToken: string) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    this.loggedIn.set(true);
  }

  private getAccessToken() {
    return localStorage.getItem('accessToken');
  }

  private getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }

  private clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.loggedIn.set(false);
  }
}
