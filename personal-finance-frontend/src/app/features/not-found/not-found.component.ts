import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950 px-4">
      <div class="text-center">
        <p class="text-8xl font-bold text-indigo-600 dark:text-indigo-400">404</p>
        <h1 class="mt-4 text-2xl font-bold text-gray-900 dark:text-gray-100">Page not found</h1>
        <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
          The page you're looking for doesn't exist.
        </p>
        <a
          routerLink="/dashboard"
          class="mt-8 inline-flex items-center px-4 py-2 bg-indigo-600 dark:bg-indigo-500 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 dark:hover:bg-indigo-400 transition-colors"
        >
          Go to Dashboard
        </a>
      </div>
    </div>
  `,
})
export class NotFoundComponent {}

