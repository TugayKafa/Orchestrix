import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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

  register(email: string, password: string, firstName: string, lastName: string) {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, { email, password, firstName, lastName });
  }

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password });
  }

  refresh(token: string) {
    return this.http.post<AccessTokenResponse>(`${this.apiUrl}/refresh`, { token });
  }

  logout(token: string) {
    return this.http.post<void>(`${this.apiUrl}/logout`, { token });
  }
}
