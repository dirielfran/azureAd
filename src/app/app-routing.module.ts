import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { ProtectedDataComponent } from './components/protected-data.component';

const routes: Routes = [
  { path: '', redirectTo: '/protected-data', pathMatch: 'full' },
  { path: 'protected-data', component: ProtectedDataComponent, canActivate: [MsalGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    initialNavigation: 'enabledBlocking'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }