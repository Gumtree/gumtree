'''
Taipan BE-Fiter Analysis

This script is for analysis of data from Taipan BE-filter experiments. It follows the procedure outlined in "Data-processing technique for the Taipan Be-filter neutron spectrometer at the Australian Nuclear Science and Technology Organisation" (2021).

The script below imports the data from its .xys format. It then processes the data with a background subtraction as well as higher-order scattering corrections. It does this for both the PG and Cu monochromator scans.

There is one part which accounts for unevenly spaced data in the energy domain if this is an issue.
Data importing:

Enter the file names in this cell
'''

#Import required modules
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from tkinter import*
from tkinter import filedialog
import tkinter.simpledialog as tkSimpleDialog
# from aifc import data
root = Tk()     # Create Tk root

#Define the name of the data directory, and define the header names for the data (initial energy, singal intensity, and error).
folder_name = "Normalised/"
folder_name = "W:/Taipan/data/Kirrily/inputs/"
head_names = ["Ei", "signal", "sigma"]




def import_n_fns(*fns):
    if isinstance(fns, str):
        fns = (fns,)  # Convert single DataFrame to a tuple with one element if only one non-tuple arg is input

    df = pd.DataFrame()
    for i in fns:
        directory_name = folder_name if i[:2] != 'C:' else ''

        df_single = pd.read_csv(directory_name+i, skiprows=0, delim_whitespace=True, header=None, names = head_names) #Depending on data file structure, may need to skip metadata of 19 rows
        df = df.append(df_single)

    return df.iloc[::-1].sort_values('Ei').reset_index(drop='True')                                                   # Reverse energy order since the data is taken from high to low energy, 
    #return df



def directory_via_gui(title_name):            #Function which brings up a GUI for selecting files
    # Hide the main window
    root.withdraw()
    root.call('wm', 'attributes', '.', '-topmost', True)


    infiles = filedialog.askopenfilename(multiple=True, title=title_name)
    file_list = list(infiles)

    return file_list




def sample_name_input(name_type):
    root.withdraw()
    root.call('wm', 'attributes', '.', '-topmost', True)
    S_name = tkSimpleDialog.askstring("Sample Name Input", "Please input the sample "+name_type+":")

    return S_name

#SAMPLE_PG_FILES = ["103692.xys", "103693.xys"]
#SAMPLE_Cu_FILES = ["103694.xys", "103695.xys", "103696.xys"]

EMPTY_PG_FILES = ["103654.xys", "103655.xys"]
EMPTY_Cu_FILES = ["103656.xys", "103657.xys", "103658.xys"]

EMPTY_PG_FILES = ["103654_4.xys", "103655_3.xys"]
EMPTY_Cu_FILES = ["103656_2.xys", "103657_1.xys", "103658_0.xys"]

# SAMPLE_PG_FILES = directory_via_gui('Select your PG sample files')
# SAMPLE_Cu_FILES = directory_via_gui('Select your Cu sample files')

SAMPLE_PG_FILES = ["103731_18.xys", "103732_17.xys", "103733_16.xys", "103734_15.xys"]
SAMPLE_Cu_FILES = ["103735_14.xys", "103736_13.xys", "103737_12.xys", "103738_11.xys", "103739_10.xys", "103740_9.xys"]

#EMPTY_PG_FILES = directory_via_gui('Select your PG empty files')
#EMPTY_Cu_FILES = directory_via_gui('Select your Cu empty files')



#Now I import the files associated with a particular measurement.
SMPL_PG = import_n_fns(*SAMPLE_PG_FILES)
SMPL_Cu = import_n_fns(*SAMPLE_Cu_FILES)

#Of course, the associated background scans must be loaded in as well.
EMPTY_PG = import_n_fns(*EMPTY_PG_FILES)
EMPTY_Cu = import_n_fns(*EMPTY_Cu_FILES)



#NAME YOUR SAMPLE HERE!:
Sample_name = 'H2O'     #Name the sample
Sample_temp = '5K'      #Temp

# Sample_name = sample_name_input('name')
# Sample_temp = sample_name_input('temperature')

# Sample_name = 'D2O'
# Sample_temp = '5K'

#Let's give some names to our wonderful measurements; they've earnt it.
SMPL_PG.name = Sample_name + '_' + Sample_temp + '_PG'
SMPL_Cu.name = Sample_name + '_' + Sample_temp + '_Cu'

'''
Rescale background scans to match sample scans:

If mismatched energy steps between background and sample measurements is an issue, the cell below is formatted to solve this issue
'''

#The background scans and the sample scans below 26.81 meV doe not match in energy steps (...oops). So here, I upsample the background scan to be the same length as the sample scan over this region.
#If this is not an issue with the given dataset, this cell can be skipped.

PG_LOW = 60 #Define the threshold (in energy) for the last PG scans.


#Now let's rescale (via linear interpolation) the signal and the sigma of the empty low E scan to match the energy steps of the sample scan. 
int_signal = np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,1][EMPTY_PG['Ei'] < PG_LOW])
int_err = np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,2][EMPTY_PG['Ei'] < PG_LOW])

#Make a new dataframe for the empty scan where the low E data is replaced with the rescaled data.
EMPTY_PG_interp = pd.concat([EMPTY_PG[EMPTY_PG['Ei'] > PG_LOW], 
                  pd.DataFrame(np.transpose(np.array([SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], int_signal, int_err])),
                  columns=head_names)]).sort_values('Ei').reset_index(drop='True')

#EMPTY_PG = EMPTY_PG_interp #Set the interpolated dataset as the main exmpty scan to be used through the rest of the processing

#Rescaling Cu Background

Cu_LOW = 200 #Define the threshold (in energy) for the last Cu scans.


#Now let's rescale (via linear interpolation) the signal and the sigma of the empty low E scan to match the energy steps of the sample scan. 
int_signal = np.interp(SMPL_Cu.iloc[:,0][SMPL_Cu['Ei'] < Cu_LOW], EMPTY_Cu.iloc[:,0][EMPTY_Cu['Ei'] < Cu_LOW], EMPTY_Cu.iloc[:,1][EMPTY_Cu['Ei'] < Cu_LOW])
int_err = np.interp(SMPL_Cu.iloc[:,0][SMPL_Cu['Ei'] < Cu_LOW], EMPTY_Cu.iloc[:,0][EMPTY_Cu['Ei'] < Cu_LOW], EMPTY_Cu.iloc[:,2][EMPTY_Cu['Ei'] < Cu_LOW])

#Make a new dataframe for the empty scan where the low E data is replaced with the rescaled data.
EMPTY_Cu_interp = pd.concat([EMPTY_Cu[EMPTY_Cu['Ei'] > Cu_LOW], 
                  pd.DataFrame(np.transpose(np.array([SMPL_Cu.iloc[:,0][SMPL_Cu['Ei'] < Cu_LOW], int_signal, int_err])),
                  columns=head_names)]).sort_values('Ei').reset_index(drop='True')

EMPTY_Cu = EMPTY_Cu_interp #Set the interpolated dataset as the main exmpty scan to be used through the rest of the processing

'''
Energy correction:

Here we set up for the energy correction
'''

#Define the variables for calculations bewteen Ei and dE and deltaE
h = 6.626E-34           #kg m2 s-2 s
m_n = 1.674929E-27      #kg
e = 1.60217662E-19      #C
k = 1.38064852E-23      #m2 kg s-2 K-1
dE_f = 0.0005           #eV
Ef = 0.0012             #eV
dtheta = np.radians(0.25)   #rad
T = 293                 #K
d_PG002 = 3.354E-10     #m
d_Cu200 = 1.807E-10     #m

#Function which converts Ei to dE
def Ei_to_DeltaE(Ei,d):
    
    Lambda = np.sqrt(h**2/(2*m_n*(Ei*e/1000)))      #Calculates Lambda in m
    theta = np.arcsin(Lambda*1/(2*d))               #Calculates scattering angle theta in radians
    dLambda = Lambda*1/(np.tan(theta))*dtheta       #Calculates dLamnda in m
    dEi = -h**2/(2*m_n)*dLambda/(Lambda**3)/e       #Calculates dEi in eV
    return pd.DataFrame(np.sqrt(dEi**2 + dE_f**2)*1000)           #Calculates dE in meV
    

#Make dataframes for dE to be later used for energy correction. This is done for both the PG and Cu monos.
dE_PG = Ei_to_DeltaE(SMPL_PG['Ei'],d_PG002).rename(columns = {'Ei': 'dE'}) 
dE_Cu = Ei_to_DeltaE(SMPL_Cu['Ei'],d_Cu200).rename(columns = {'Ei': 'dE'})  #We simply execute the function above and relabel the column as 'dE'.

#While we're here, let's make dataframes for DeltaE (energy transfer). This is what we will finally plot as our independent var.
SMPL_PG['DeltaE'] = SMPL_PG['Ei'] - 1000*Ef
SMPL_Cu['DeltaE'] = SMPL_Cu['Ei'] - 1000*Ef #Here we simply compute Ei-Ef for PG and Cu measurements.

'''
Setting up the energy correction and background subtraction:
'''
#Exponential correction function paramters:
A = 9.64047
t = 4.94769
y0 = 1.05681

#Background subtraction and signal correction from energy uncertainty, and higher-order exponential correction function
def bkg_subtraction(samp_data, bkg_data, dE_data):
    samp_data['signal_bkg_sub'] = samp_data['signal'] - bkg_data['signal']                                              #Background subraction
    samp_data['signal_ECorr'] = (samp_data['signal_bkg_sub'] / dE_data['dE']) / (A*np.exp(-samp_data['Ei']/t) + y0)     #Exponential correction
    samp_data['Fin_Lam_Corr'] = samp_data['signal_ECorr']                                                                   #Here, we are just setting up a new column from which 
                                                                                                                            #we will subract the high-order scattering contamination
'''
Wavelength contamination removal:

Excecute the background subtraction and exponential function correction, then set up and execute the wavelength contamination removal
'''
#We now apply the corrections from the funciton above to both the PG and Cu measurements:
bkg_subtraction(SMPL_PG, EMPTY_PG, dE_PG)
bkg_subtraction(SMPL_Cu, EMPTY_Cu, dE_Cu)

#####------------------------------------------------------------------------------------------------------------------------------------------------#####
#Wavelength contamination removal:

#Make a function which corrects the higher order scattering.
#This is the main reason I automated the process in the first place. The energy steps at the higher energies (supplying the corrections)
#are not necessarily the same as the lower enery steps. So once the correction has been generated, it must be rescaled and interpolated
#to match the energy steps of the actual data being corrected. This is what the function below does.
#Note that mod should be set to 1, unless some of the corrected intensity drops below 0, then adjust as needed to keep data above 0.

def higher_order_corr(Data_to_correct, correcting_data, scatter_order, mod):    #We input the data to correct, and a separate correcting data so tha Cu can correct PG

    corr_name = 'lam_'+str(scatter_order)+'_corr_from_'+correcting_data.name    #Define the name of the data-to-correct, including the order of scattering correction
                                                                                #to track the correction being made

    Data_to_correct[corr_name] = pd.DataFrame(np.interp(Data_to_correct['Ei'][Data_to_correct['Ei'] < correcting_data['Ei'].max() / scatter_order**2],  
    correcting_data['Ei'] / scatter_order**2,                                                                                                               
    correcting_data['signal_ECorr'] / scatter_order**2 / mod))
    #This above is the meaty part. A new column in the sample dataframe is made which includes higher-energy data which is modified in intensity to correct
    #higher-order scattering effects at the lower energies. Thus, this data is also scaled and interpolated to match the energy steps at the lower-energy
    #of the data we are correcting, so that the correction actually works. 
                                                                                    
    Data_to_correct[corr_name] = Data_to_correct[corr_name].fillna(0)                                   #This line changes NaNs (values above the E/n threshold) to 0s 
    Data_to_correct['Fin_Lam_Corr'] = Data_to_correct['Fin_Lam_Corr'] - Data_to_correct[corr_name]      #Final correction by subracting the correction data from the final intensity



#Now we apply the function to all the necessary data. One will have to manually check the highest order of scattering 
#which can be corrected based on the energy range of the scans:

#Self-corrections:
higher_order_corr(SMPL_PG, SMPL_PG, 2, 1)
higher_order_corr(SMPL_Cu, SMPL_Cu, 2, 1)

#Cu correcting PG:
higher_order_corr(SMPL_PG, SMPL_Cu, 2, 1.1)
higher_order_corr(SMPL_PG, SMPL_Cu, 3, 1.1)



#Some lines to check the outputs:

####---This is how you reorder the labels for the DataFrame if desired:---####
#SMPL_PG=SMPL_PG[['DeltaE','Ei','signal','sigma','signal_bkg_sub','signal_ECorr','lam_2_corr_from_LA4K_PG','lam_2_corr_from_LA4K_Cu','lam_3_corr_from_LA4K_Cu','Fin_Lam_Corr']]

####---You can check the DataFrames to make sure they look ok, and if modifer is appropriate:---####
#SMPL_PG
#SMPL_Cu

###---You can check the DataFrames without row cutoff:---####
# with pd.option_context('display.max_rows', None, 'display.max_columns', None,'display.precision', 5,):
#    print(SMPL_PG)
'''
Error bars!
'''
#Can't forget the error bars! (Even though they're not that significant here)
 
#For the PG
SMPL_PG['signal_bkg_sub_SIGMA'] = np.sqrt(SMPL_PG['sigma']**2 + EMPTY_PG['sigma']**2)
SMPL_PG['Fin_Lam_Corr_SIGMA'] = SMPL_PG['Fin_Lam_Corr'] * (SMPL_PG['signal_bkg_sub_SIGMA'] / SMPL_PG['signal_bkg_sub'])
# 
# #For the Cu
SMPL_Cu['signal_bkg_sub_SIGMA'] = np.sqrt(SMPL_Cu['sigma']**2 + EMPTY_Cu['sigma']**2)
SMPL_Cu['Fin_Lam_Corr_SIGMA'] = SMPL_Cu['Fin_Lam_Corr'] * (SMPL_Cu['signal_bkg_sub_SIGMA'] / SMPL_Cu['signal_bkg_sub'])
'''
Exporting the data:
'''
#Let's export the data to csv so we can plot it wherever we want
 
#Folder in which to save data
save_folder = 'W:/Taipan/data/Kirrily/exports/'
 
#For PG data
SMPL_PG.to_csv(save_folder+SMPL_PG.name+'_DATA.csv', sep = ',')
#For Cu data
SMPL_Cu.to_csv(save_folder+SMPL_Cu.name+'_DATA.csv', sep = ',')
# 
# '''
# Plot data
# '''
# # plt.errorbar(SMPL_Cu['DeltaE'], SMPL_Cu['Fin_Lam_Corr'], yerr = SMPL_Cu['Fin_Lam_Corr_SIGMA'], color = 'r')
# # plt.errorbar(SMPL_PG['DeltaE'], SMPL_PG['Fin_Lam_Corr'], yerr = SMPL_PG['Fin_Lam_Corr_SIGMA'], color = 'black')
# plt.plot(SMPL_Cu['DeltaE'], SMPL_Cu['Fin_Lam_Corr'], '-o', markersize=4, label = 'Cu', color = 'r')
# plt.plot(SMPL_PG['DeltaE'], SMPL_PG['Fin_Lam_Corr'], '-o', markersize=4,  label = 'PG', color = 'black')
# plt.plot(SMPL_PG['DeltaE'], SMPL_PG['signal'], '-o', markersize=4,  label = 'PG-unproc.', color = 'b')
# # plt.plot(SMPL_PG['DeltaE'], EMPTY_PG['signal']*6, '-o', markersize=4,  label = 'PG-empty.', color = 'g')
# plt.legend()
# # plt.xlim([8.9,70])
# # plt.ylim([40000,285000])
# # plt.ylim([2000,20000])
# 
# plt.xlabel("Energy (meV)")
# plt.ylabel('Intensity (arb.u.)')
# 
# # plt.scatter(SMPL_Cu['Ei'],SMPL_Cu['signal'])
# # plt.scatter(SMPL_Cu['Ei'],SMPL_Cu['signal_bkg_sub'])
# # 
# # plt.scatter(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < SMPL_PG['Ei'].max()/4], np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < SMPL_PG['Ei'].max()/4], SMPL_PG.iloc[:,0]/4, SMPL_PG.iloc[:,1] /4 ))
# # plt.scatter(SMPL_PG.iloc[:,0]/4, SMPL_PG.iloc[:,1] /4 )
# # plt.xlim([8.9,17.5])
# 
# plt.show()
# PG_LOW = 26.82
# EMPTY_PG = import_fn("95672.xys", "95673.xys", "95674.xys")

# int_signal = np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,1][EMPTY_PG['Ei'] < PG_LOW])

# plt.scatter(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], int_signal)
# plt.scatter(EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,1][EMPTY_PG['Ei'] < PG_LOW])


fig, ax = plt.subplots(figsize=(10, 6))

ax.errorbar(SMPL_Cu['DeltaE'], SMPL_Cu['Fin_Lam_Corr'], fmt='-o', ms = 4, capsize = 3,  yerr = SMPL_Cu['Fin_Lam_Corr_SIGMA'], color = 'r', label = 'Cu')
ax.errorbar(SMPL_PG['DeltaE'], SMPL_PG['Fin_Lam_Corr'], fmt='-o', ms = 4, capsize = 3, yerr = SMPL_PG['Fin_Lam_Corr_SIGMA'], color = 'black', label = 'PG')
ax.plot(SMPL_Cu['DeltaE'], SMPL_Cu['signal'], '-o', markersize=4,  label = 'Cu-unproc.', color = 'magenta', alpha = 0.33)
ax.plot(SMPL_Cu['DeltaE'], EMPTY_Cu['signal']*1, '-o', markersize=4,  label = 'Cu-empty', color = 'orange', alpha = 0.33)
ax.plot(SMPL_PG['DeltaE'], SMPL_PG['signal'], '-o', markersize=4,  label = 'PG-unproc.', color = 'b', alpha = 0.33)
# ax.plot(SMPL_PG['DeltaE'], EMPTY_PG['signal']*1, '-o', markersize=4,  label = 'PG-empty', color = 'g', alpha = 0.33)
ax.legend()
ax.set_xlim([0,205])
ax.set_ylim([-10000,75000])
ax.set_xlabel("Energy (meV)")
ax.set_ylabel('Intensity (arb.u.)')

for item in ([ax.title, ax.xaxis.label, ax.yaxis.label,] +
             ax.get_xticklabels() + ax.get_yticklabels() ):
    item.set_fontsize(20)
    
for item in (ax.get_legend().get_texts()):
    item.set_fontsize(14)
    
plt.show()
    