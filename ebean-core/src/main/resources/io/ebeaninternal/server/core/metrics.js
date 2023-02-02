/**
 * The sort code is adapted from https://github.com/tofsjonas/sortable
 * Copyleft 2017 Jonas Earendel
 * License: http://unlicense.org
 */

 (function() { // start closure
function findElementRecursive(element, tag) {
 	return !element ? null : element.nodeName === tag ? element : findElementRecursive(element.parentNode, tag)
}

function getValue(element) {
	return element.getAttribute('data-sort') || element.innerText
}

document.addEventListener('click', function (e) {
	try {
		var descending_th_class = ' dir-d '
		var ascending_th_class = ' dir-u '
		var ascending_table_sort_class = 'asc'
		var regex_dir = / dir-(u|d) /
		var element = findElementRecursive(e.target, 'TH')
		var tr = findElementRecursive(element, 'TR')
		var table = findElementRecursive(tr, 'TABLE')

		function reClassify(element, dir) {
			element.className = element.className.replace(regex_dir, '') + dir
		}

		if (table && tr && tr.className == 'sortHdr') {
			var column_index
			var nodes = tr.cells
			// Reset thead cells and get column index
			for (var i = 0; i < nodes.length; i++) {
				if (nodes[i] === element) {
					column_index = i
				} else {
					reClassify(nodes[i], '')
				}
			}
			var dir = descending_th_class

			// Check if we're sorting ascending or descending
			if (element.className.indexOf(descending_th_class) !== -1
				|| (table.className.indexOf(ascending_table_sort_class) !== -1
					&& element.className.indexOf(ascending_th_class) == -1)) {
				dir = ascending_th_class
			}

			// Update the `th` class accordingly
			reClassify(element, dir)

			// Get the array rows in an array, so we can sort them...
			var rows = [].slice.call(table.tBodies[0].rows, 0)
			var reverse = dir === ascending_th_class

			// Sort them using Array.prototype.sort()
			rows.sort(function (a, b) {
				var x = getValue((reverse ? a : b).cells[column_index])
				var y = getValue((reverse ? b : a).cells[column_index])
				return x.length && y.length && !isNaN(x - y) ? x - y : x.localeCompare(y)
			})

			// Make a clone without content
			var clone_tbody = table.tBodies[0].cloneNode()
			// Fill it with the sorted values
			while (rows.length) {
				clone_tbody.appendChild(rows.splice(0, 1)[0])
			}
			// And finally replace the unsorted table with the sorted one
			table.replaceChild(clone_tbody, table.tBodies[0])
		}
	} catch (error) {
		console.log(error)
	}
})

function getIndex(collection, element) {
	for (var i = 0; i < collection.length; i++) {
		if (collection[i] === element) return i;
	}
	return -1;
}

function updateFilterValues() {
	for (var i = 0; i < filters.length; i++) {
		var filter = filters[i];
		var table = findElementRecursive(filter, 'TABLE')
		var cell = findElementRecursive(filter, 'TH')
		var tr = findElementRecursive(filter, 'TR')
		var idx = getIndex(tr.cells, cell)
		var option = document.createElement("option");
		option.text = "-";
		[].slice.call(filter.options, 0).forEach(e => e.remove());
		filter.add(option);
		var set = new Set();
		const rows = table.tBodies[0].rows;
		for (var k = 0; k < rows.length; k++) {
			const txt = rows[k].cells[idx].innerText;
			if (txt) set.add(txt);
		}
		Array.from(set).sort().forEach(item => {
			var option = document.createElement("option");
			option.text = item;
			filter.add(option);
		});
	}
}
function matchFilter(row, filters) {
	for (var i = 0; i < filters.length; i++) {
		var filter = filters[i];
		if (filter.value != "-") {
			if (row.cells[filter.getAttribute('data-idx')].innerText != filter.value) {
				return false;
			}
		}
	}
	return true;
}
function applyFilters(filter) {
	var table = findElementRecursive(filter, 'TABLE')
	var filters = table.getElementsByTagName("SELECT");
	var tBody = table.tBodies[0];
	var spareBody = table.tBodies[1];

	var rows = [].slice.call(tBody.rows, 0).concat([].slice.call(spareBody.rows));
	var clone_tbody = tBody.cloneNode()
	var clone_sparebody = spareBody.cloneNode()
	// Fill it with the sorted values
	while (rows.length) {
		var row = rows.splice(0, 1)[0];
		if (matchFilter(row, filters)) {
			clone_tbody.appendChild(row);
		} else {
			clone_sparebody.appendChild(row);
		}
	}

	table.replaceChild(clone_tbody, tBody)
	table.replaceChild(clone_sparebody, spareBody)
}

function initFilter(filter) {
	var table = findElementRecursive(filter, 'TABLE')
	var cell = findElementRecursive(filter, 'TH')
	var tr = findElementRecursive(filter, 'TR')
	var idx = getIndex(tr.cells, cell)

	var spare = document.createElement("table");
	spare.style="display:none";
	cell.appendChild(spare);
	filter.onchange = evt => applyFilters(evt.target)
	filter.setAttribute('data-idx', idx)

	var option = document.createElement("option");
	option.text = "-";
	[].slice.call(filter.options, 0).forEach(e => e.remove());
	filter.add(option);
	var set = new Set();
	const rows = table.tBodies[0].rows;
	for (var k = 0; k < rows.length; k++) {
		const txt = rows[k].cells[idx].innerText;
		if (txt) set.add(txt);
	}
	Array.from(set).sort().forEach(item => {
		var option = document.createElement("option");
		option.text = item;
		filter.add(option);
	});
}

var filters = document.getElementsByTagName("SELECT")
for (var i = 0; i < filters.length; i++) initFilter(filters[i]);
})();

/**
 * used to communicate with 'configure' endpoint
 */
function updateValue(element) {
	var xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = function() {
		if (this.readyState == 4) {
			if (this.status == 200) {
				if (this.responseText == "OK") {
					return;
				}
				if (this.responseText == "REFRESH") {
					if (confirm("Action executed successfully. Reload pages?")) {
						document.location.reload();
					}
					return;
				}
			}
			alert("An error occured: " + this.responseText);
		}
	};
	xmlhttp.open("POST", "configure", false);
	xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	var value = element.value;
	if (element.type == 'checkbox') {
		value = element.checked ? 1 : 0;
	}
	xmlhttp.send(JSON.stringify([{'name' : element.name , 'value' : value}]));

}
