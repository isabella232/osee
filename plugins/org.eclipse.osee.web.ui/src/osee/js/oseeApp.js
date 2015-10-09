var app = angular.module('oseeApp', ['oauth', 'ngResource', 'ui.bootstrap', 'oseeProvider', 'ngRoute', 'aLinkRewrite', 'ui.grid', 'ui.grid.edit']);

app.config(['$routeProvider',
                         function($routeProvider) {
                         	
                           $routeProvider.when('/', {
                         	  templateUrl: '../views/home.html',
                               controller: 'indexController'
                           }).when('/wiki', {
                         	  templateUrl: '../views/wiki.html',
                               controller: 'indexController'
                           }).when('/about', {
                         	  templateUrl: '../views/about.html',
                               controller: 'indexController'
                           }).when('/contactUs', {
                         	  templateUrl: '../views/contactUs.html',
                               controller: 'indexController'
                           }).when('/me', {
                         	  templateUrl: '../views/me.html',
                               controller: 'meController'
                           }).when('/redirect', {
                         	  templateUrl: '../views/home.html',
                               controller: 'indexController'
                           })
                           .otherwise({
                             redirectTo: "/"
                             });
                           }
                         ]);
                         
app.provider('Preferences', function() {
    this.$get = ['$resource',
        function($resource) {
            var Preferences = $resource('/accounts/preferences/:id', {}, {
            	update: {
                    method: 'PUT',
                    headers: {
                        "Accept": "application/json"
                    }
                }
            });
            return Preferences;
        }
    ];
});