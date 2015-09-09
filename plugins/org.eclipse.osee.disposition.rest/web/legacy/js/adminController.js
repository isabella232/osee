		app.controller('adminController', ['$scope', '$rootScope', '$modal', 'Program', 'Set', 'Report', 'CopySet',
		    function($scope, $rootScope,  $modal, Program, Set, Report, CopySet) {
		        $scope.readOnly = true;
		        $scope.programSelection = null;
		        $scope.modalShown = false;
		        $scope.primarySet = "";
		        $scope.secondarySet = "";
		        $scope.sets = {};
			     $scope.addNew = false;
		        $scope.newProgramName = ""
		        $scope.isCopying = false;

		        $scope.cachedValue = "";

		        $scope.$on('ngGridEventStartCellEdit', function(data) {
		            var field = data.targetScope.col.field;
		            $scope.cachedValue = data.targetScope.row.getProperty(field);
		        });

		        $scope.$on('ngGridEventEndCellEdit', function(data) {
		            var field = data.targetScope.col.field;
		            var row = data.targetScope.row;
		            var newValue = row.getProperty(field);

		            if ($scope.cachedValue != newValue) {
		                $scope.editSet(row.entity);
		            }
		        });



		        $scope.gridOptions = {
		            data: 'sets',
		            enableHighlighting: true,
		            enableColumnResize: false,
		            enableRowReordering: true,
		            multiSelect: false,
		            columnDefs: 'columnDefs' // link to scope variable which we will define dynamically				
		        }

		        var editCellTmpl = '<input editable="true" >'
		        var dellCellTmpl = '<button width="50px" class="btn btn-default btn-sm setDelete" ng-show="!readOnly" ng-click="deleteSet(row.entity)">X</button>';
		        var importCellTmpl = '<button width="50px" class="btn btn-primary" ng-disabled="row.entity.processingImport" ng-click="importSet(row.entity)">Import</button>';
		        var exportCellTmpl = '<button width="50px" class="btn btn-primary" ng-disabled="row.entity.processingImport" ng-click="exportSet(row.entity)">Export</button>';

		        $scope.columnDefs1 = [{
		            field: "",
		            displayName: "Import",
		            width: 70,
		            enableCellEdit: false,
		            cellTemplate: importCellTmpl
		        }, {
		        	field: "",
		        	displayName: "Export",
		        	width: 70,
		        	cellTemplate: exportCellTmpl
		        }, {
		            field: "name",
		            displayName: "Name",
		            width: 200,
		            enableCellEdit: false
		        }, {
		            field: "importPath",
		            displayName: "Path",
		            width: 460,
		            enableCellEdit: false
		        }];

		        $scope.columnDefs2 = [{
		            field: "",
		            displayName: "Import",
		            width: 70,
		            enableCellEdit: false,
		            cellTemplate: importCellTmpl
		        }, {
		        	field: "",
		        	displayName: "Export",
		        	width: 70,
		        	cellTemplate: exportCellTmpl
		        },{
		            field: "name",
		            displayName: "Name",
		            width: 200,
		            enableCellEdit: true
		        }, {
		            field: "importPath",
		            displayName: "Path",
		            width: 413,
		            enableCellEdit: true
		        }, {
		            field: "delete",
		            displayName: "Delete",
		            width: 57,
		            cellTemplate: dellCellTmpl
		        }];
		        
		        $scope.createNewProgram = function() {
		            if ($scope.newProgramName != "") {
		        		    var loadingModal = $scope.showLoadingModal();
		                var newProgram = new Program;
		                newProgram.name = $scope.newProgramName;
		                newProgram.$save({
		                    name: $scope.newProgramName
		                }, function() {
		                 		$scope.newProgramName = "";
		                		$scope.addNew = false;
		                	  loadingModal.close();
		                    $scope.programs = Program.query();
		                }, function() {
		                	  loadingModal.close();
		                    alert("Oops...Something went wrong");
		                });
		            }
		        }
		        
		        $scope.toggleAddNew = function() {
		        		if($scope.addNew) {
		        			 $scope.addNew = false;
		        		} else {
		        			$scope.addNew = true;
		        		}
		        }

		        $scope.columnDefs = $scope.columnDefs1;

		        $scope.programs = Program.query();

		        $scope.toggleModal = function() {
		            $scope.modalShown = !$scope.modalShown
		        };

		        $scope.toggleReadOnly = function() {
		            if ($scope.readOnly) {
		                $scope.columnDefs = $scope.columnDefs2;
		                $scope.readOnly = false;
		            } else {
		                $scope.columnDefs = $scope.columnDefs1;
		                $scope.readOnly = true;
		            }

		        };

		        $scope.generateReport = function() {
		        	var requst = [];
		        	requst.push(
		        	  "/dispo/program/",
		        	  $scope.programSelection,
		        	  "/admin/report?primarySet=",
		        	  $scope.primarySet,
		        	  "&secondarySet=",
		        	  $scope.secondarySet
		        	  );
		        	var url = requst.join("");
		            
		            window.open(url);
		        }
		        
		        
		        
		        $scope.updateProgram = function updateProgram() {
		        		var loadingModal = $scope.showLoadingModal();
		            $scope.loading = true;
			        	$scope.items = {};
			            Set.query({
			                programId: $scope.programSelection,
			                type: $rootScope.type
			            }, function(data) {
			            	loadingModal.close();
			                $scope.sets = data;
			            }, function(data) {
			            	loadingModal.close();
			            	alert(data.statusText);
			            });
		        };

		        $scope.editSet = function editSet(set) {
		            Set.update({
		                programId: $scope.programSelection,
		                setId: set.guid
		            }, set);
		        };

		        $scope.deleteSet = function deleteSet(set) {
		            Set.delete({
		                programId: $scope.programSelection,
		                setId: set.guid
		            }, function() {
		                var index = $scope.sets.indexOf(set);
		                if (index > -1) {
		                    $scope.sets.splice(index, 1);
		                }
		            });

		        }


		        $scope.importSet = function importSet(set) {
		        	console.log(new Date().getTime());
		            var newSet = new Set;
		            newSet.operation = "Import";
		            set.processingImport = true;
		            Set.update({
		                programId: $scope.programSelection,
		                setId: set.guid
		            }, newSet, function(data){
		            	var reportUrl = data.operationStatus;
			            window.open(reportUrl);
		            	console.log(data);
		            	set.processingImport = false;
		            }, function() {
		            	console.log("Failed");
		            	console.log(new Date().getTime());
		            	set.processingImport = false;
		            	alert("Could not Import");
		            });
		        };
		        
		        $scope.exportSet = function importSet(set) {
		        	var requst = [];
		        	requst.push(
		        	  "/dispo/program/",
		        	  $scope.programSelection,
		        	  "/admin/export?primarySet=",
		        	  set.guid,
		        	  "&option=detailed"
		        	  );
		        	var url = requst.join("");
		            
		            window.open(url);
		        };

		        $scope.createNewSet = function createNewSet(name, path) {
		            if (name != "" && path != "") {
		                var newSet = new Set;
		                newSet.name = name;
		                newSet.importPath = path;
		                newSet.dispoType = $rootScope.type;
		                newSet.$save({
		                    programId: $scope.programSelection
		                }, function(data) {
		                    $scope.sets.push(data);
		                });
		            }
		        };
		        
		        $scope.copySet = function(inputs)	 {
		        	$scope.isCopying = true;
		        	var copySetOp = new CopySet;
		        	copySetOp.annotationParam = inputs.annotationParam;
		        	copySetOp.categoryParam = inputs.categoryParam;
		        	copySetOp.assigneeParam = inputs.assigneeParam;
		        	copySetOp.noteParam = inputs.noteParam;
		        	
		        	copySetOp.$save({
		                programId: $scope.programSelection,
		                destinationSet: inputs.destinationSet,
		                sourceSet: inputs.sourceSet,
		            }, function(data) {
		            	$scope.isCopying = false;
		            	var reportUrl = data.operationStatus;
			            window.open(reportUrl);
		            	console.log(data);
		            }, function(data) {
		               $scope.isCopying = false;
		            	alert("Oops...Something went wrong");
		            });
		        }
		        
		        // Loading Modal
		        $scope.showLoadingModal = function() {
		            var modalInstance = $modal.open({
		                templateUrl: 'loadingModal.html',
		                size: 'sm',
		                windowClass: 'needsRerunModal',
		                backdrop: 'static'
		            });
		            
		            return modalInstance;
		        }

		        // Create Set Modal
		        $scope.createNewSetModal = function() {
		            var modalInstance = $modal.open({
		                templateUrl: 'popup.html',
		                controller: CreateSetModalCtrl,
		                size: 'sm',
		                windowClass: 'createSetModal'
		            });

		            modalInstance.result.then(function(inputs) {
		                $scope.createNewSet(inputs.name, inputs.path);
		            });
		        }

		        var CreateSetModalCtrl = function($scope, $modalInstance) {
		            $scope.setName = "";
		            $scope.importPath = "";
		            
		            $scope.ok = function() {
		                var inputs = {};
		                inputs.name = this.setName;
		                inputs.path = this.importPath;
		                $modalInstance.close(inputs);
		            };

		            $scope.cancel = function() {
		                $modalInstance.dismiss('cancel');
		            };
		        };
		        
		        // Copy Set Modal
		        $scope.openCopySetModal = function() {
		            var modalInstance = $modal.open({
		                templateUrl: 'copySets.html',
		                controller: CopySetModalCtrl,
		                size: 'md',
		                windowClass: 'copySetModal',
		                resolve: {
		                	sets: function() {
		                		return $scope.sets;
		                	}
		                }
		            });

		            modalInstance.result.then(function(inputs) {
		                $scope.copySet(inputs);
		            });
		        }
		        
		        
		        var CopySetModalCtrl = function($scope, $modalInstance, sets) {
		            $scope.setsLocal = angular.copy(sets);
		            $scope.annotationOptions = [{ value: 0, text: 'NONE'}, { value: 1, text: 'OVERRIDE'}];
		            $scope.categoryOptions = [{ value: 0, text: 'NONE'}, { value: 1, text: 'OVERRIDE'}, { value: 2, text: 'ONLY COPY IF DEST IS EMPTY'}, { value: 3, text: 'MERGE DEST AND SOURCE'}];
		            $scope.assigneeOptions = [{ value: 0, text: 'NONE'}, { value: 1, text: 'OVERRIDE'}, { value: 2, text: 'ONLY COPY IF DEST IS UNASSIGNED'}];
		            $scope.noteOptions = [{ value: 0, text: 'NONE'}, { value: 1, text: 'OVERRIDE'}, { value: 2, text: 'ONLY COPY IF DEST IS EMPTY'}, { value: 3, text: 'MERGE DEST AND SOURCE'}];
		            
		            $scope.annotationParam = 0;
		            $scope.categoryParam = 0;
		            $scope.assigneeParam = 0;
		            $scope.noteParam = 0;

		            $scope.ok = function() {
		                var inputs = {};
		                inputs.destinationSet = this.destinationSet;
		                inputs.sourceSet = this.sourceSet;
		                inputs.annotationParam = this.annotationParam;
		                inputs.categoryParam = this.categoryParam;
		                inputs.noteParam = this.noteParam;
		                inputs.assigneeParam = this.assigneeParam;
		                
		                $modalInstance.close(inputs);
		            };

		            $scope.cancel = function() {
		                $modalInstance.dismiss('cancel');
		            };
		        };

		    }
		]);