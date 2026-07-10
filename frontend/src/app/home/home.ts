import { AfterViewInit, Component, ElementRef, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Auth } from '../services/auth';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements AfterViewInit {
  protected auth = inject(Auth);
  private elementRef = inject(ElementRef);

  ngAfterViewInit() {
    const revealElements = this.elementRef.nativeElement.querySelectorAll('.reveal');

    const observer = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          observer.unobserve(entry.target);
        }
      }
    }, { threshold: 0.15 });

    revealElements.forEach((element: Element) => observer.observe(element));
  }
}
