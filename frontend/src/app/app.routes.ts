import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { OauthCallback } from './oauth-callback/oauth-callback';

export const routes: Routes = [
  { path: '', component: Login },
  { path: 'register', component: Register },
  { path: 'oauth-callback', component: OauthCallback },
];
